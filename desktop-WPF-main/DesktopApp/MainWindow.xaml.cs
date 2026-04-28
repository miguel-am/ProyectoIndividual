using DesktopApp.ViewModels;
using DesktopApp.Views;
using System.Windows;

namespace DesktopApp
{
    public partial class MainWindow : Window
    {
        public MainWindow() : this(false) { }
        public MainWindow(bool skipLogin = false)
        {
            InitializeComponent();

            // Mostramos la ventana de login antes de iniciar MainWindow
            if (!skipLogin)
            {
                var loginWindow = new LoginView();
                bool? loginResult = loginWindow.ShowDialog();

                if (loginResult != true)
                {
                    Application.Current.Shutdown();
                    return;
                }
            }

            this.DataContext = new MainViewModel();
        }
    }
}
